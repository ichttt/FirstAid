/*
 * FirstAid API
 * Copyright (c) 2017-2023
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ichttt.mods.firstaid.api.event;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.IDamageDistribution;
import ichttt.mods.firstaid.api.distribution.DamageDistributionBuilderFactory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;

public class RegisterDamageDistributionEvent extends FirstAidRegisterEvent {

    private final DamageDistributionBuilderFactory distributionBuilderFactory;
    private final ArrayList<Pair<Predicate<DamageSource>, IDamageDistribution>> distributionsDynamic = new ArrayList<>();
    private final Map<ResourceKey<DamageType>, IDamageDistribution> distributionsStatic = new HashMap<>();

    public RegisterDamageDistributionEvent(Level level, DamageDistributionBuilderFactory distributionBuilderFactory) {
        super(level);
        this.distributionBuilderFactory = distributionBuilderFactory;
    }

    public DamageDistributionBuilderFactory getDistributionBuilderFactory() {
        return distributionBuilderFactory;
    }

    /**
     * Binds a matcher predicate.
     * The matcher should be simple, as it will be called every time no static fitting distribution could be found.
     * Use {@link #registerDamageDistributionStatic(IDamageDistribution, DamageSource[])} whenever possible, as it is the faster option
     *
     * @param matcher The matcher to select whether this distribution should be used or not.
     *                Will only be called if no static distribution could be found
     */
    public void registerDamageDistributionDynamic(IDamageDistribution distribution, Predicate<DamageSource> matcher) {
        distributionsDynamic.add(Pair.of(matcher, distribution));
    }

    /**
     * Binds the damage source to a distribution.
     * This should be preferred over {@link #registerDamageDistributionDynamic(IDamageDistribution, Predicate)} whenever possible
     *
     * @param sources The sources that should use this distribution
     */
    public void registerDamageDistributionStatic(IDamageDistribution distribution, DamageSource... sources) {
        for (DamageSource damageSource : sources) {
            IDamageDistribution oldDistribution = distributionsStatic.put(damageSource.typeHolder().unwrapKey().orElseThrow(() -> new RuntimeException("Attempted to register a distribution for an unregistered damage source")), distribution);
            if (oldDistribution != null) {
                FirstAid.LOGGER.info("Damage Distribution override detected for source {} ({}), was {}, is {}", damageSource, damageSource.type().msgId(), oldDistribution, distribution);
            }
        }
    }

    public Map<ResourceKey<DamageType>, IDamageDistribution> getDistributionsStatic() {
        return Collections.unmodifiableMap(distributionsStatic);
    }

    public List<Pair<Predicate<DamageSource>, IDamageDistribution>> getDistributionsDynamic() {
        distributionsDynamic.trimToSize();
        return Collections.unmodifiableList(distributionsDynamic);
    }
}
